from flask import Flask, request, Response
from flask_restful import Resource, Api
import math
import numpy as np
import pandas as pd
from scipy.sparse import csr_matrix
from scipy.sparse.linalg import norm
from sklearn.neighbors import NearestNeighbors
import sys, psycopg2, pickle


app = Flask(__name__)
api = Api(app)
file_path = "./cache/"
rating_file_path = file_path + "ratings.csv"
book_file_path = file_path + "book.csv"
ADJUST_THRESHOLD = 15
NUM_NEIGHBORS = 900
k = 40
min_k = 20

# train files
book_id_to_idx_path = file_path + "book_id_to_idx.pkl"
book_idx_to_id_path = file_path + "book_idx_to_id.pkl"
user_id_to_idx_path = file_path + "user_id_to_idx.pkl"
user_idx_to_id_path = file_path + "user_idx_to_id.pkl"
indices_path = file_path + "indices.pkl"
similarities_path = file_path + "similarities.pkl"
popularity_path = file_path + "popularity.pkl"


def save_arbitrary_obj(obj, filename):
    with open(filename, 'wb+') as f:
        pickle.dump(obj, f)


def load_arbitrary_obj(filename):
    result = None
    with open(filename, 'rb') as f:
        result = pickle.load(f)
    return result


def download_file_from_postgres():
    conn = psycopg2.connect("dbname=postgres user=postgres password=example")
    cur = conn.cursor()

    sql = "COPY (SELECT * FROM rating) TO STDOUT WITH CSV DELIMITER ',' HEADER"
    with open(rating_file_path, "w", encoding="utf-8") as file:
        cur.copy_expert(sql, file)

    sql = "COPY (SELECT * FROM book) TO STDOUT WITH CSV DELIMITER ',' HEADER"
    with open(book_file_path, "w", encoding="utf-8") as file:
        cur.copy_expert(sql, file)

    conn.close()


def get_user_ratings_from_user_id(user_id):
    conn = psycopg2.connect("dbname=postgres user=postgres password=example")
    cur = conn.cursor()
    sql = "select book_id, rating from rating where user_id = " + str(user_id);

    cur.execute(sql)
    lst = cur.fetchall()
    conn.close()
    return lst


def train():
    df_ratings = pd.read_csv(rating_file_path, usecols=['user_id', 'book_id', 'rating'])

    num_users = len(df_ratings.user_id.unique())
    num_items = len(df_ratings.book_id.unique())
    print('There are {} unique users and {} unique movies in this data set'.format(num_users, num_items))

    df_books_cnt = pd.DataFrame(df_ratings.groupby('book_id').size(), columns=['count'])

    # filter data
    popularity_thres = 200
    popular_books = list(set(df_books_cnt.query('count >= @popularity_thres').index))
    df_ratings_drop_books = df_ratings[df_ratings.book_id.isin(popular_books)]
    print('shape of original ratings data: ', df_ratings.shape)
    print('shape of ratings data after dropping unpopular books: ', df_ratings_drop_books.shape)
    print('Number of popular book', len(popular_books))

    df_users_cnt = pd.DataFrame(df_ratings_drop_books.groupby('user_id').size(), columns=['count'])

    # filter data
    ratings_thres = 130
    active_users = list(set(df_users_cnt.query('count >= @ratings_thres').index))
    df_ratings_drop_users = df_ratings_drop_books[df_ratings_drop_books.user_id.isin(active_users)]
    print('shape of original ratings data: ', df_ratings.shape)
    print('shape of ratings data after dropping both unpopular movies and inactive users: ',
          df_ratings_drop_users.shape)
    print('number of active user: ', len(active_users))

    del df_ratings

    # pivot and create book-user matrix
    book_user_mat = df_ratings_drop_users.pivot(index='book_id', columns='user_id', values='rating')
    book_user_mat_standardized = (book_user_mat - np.mean(book_user_mat, axis=0)).fillna(0)
    book_user_mat_bool = ~np.isnan(book_user_mat)

    book_id_to_idx = {
        book_id: idx for idx, book_id in enumerate(book_user_mat.index)
    }

    book_idx_to_id = {v: k for k, v in book_id_to_idx.items()}

    user_id_to_idx = {
        user_id: idx for idx, user_id in enumerate(book_user_mat.columns)
    }

    user_idx_to_id = {v: k for k, v in book_id_to_idx.items()}

    # convert to sparse matrix
    book_user_mat_sparse = csr_matrix(book_user_mat_standardized.values)
    book_user_mat_bool_sparse = csr_matrix(book_user_mat_bool.values)
    del book_user_mat_standardized
    del book_user_mat_bool

    # square the matrix for weight calculation
    x_squared = book_user_mat_sparse.power(2)

    # only take the value where rating is not nan for weight calculation
    weight_left = x_squared.dot(book_user_mat_bool_sparse.T)
    del x_squared

    # calculate weight matrix
    weight_left = weight_left.toarray()
    weight = np.sqrt(weight_left * weight_left.T)
    del weight_left

    # calculate the adjust matrix
    confidence_matrix = np.dot(book_user_mat_bool_sparse, book_user_mat_bool_sparse.T.astype(int))
    adjusted_matrix = confidence_matrix / ADJUST_THRESHOLD
    adjusted_matrix[adjusted_matrix > 1] = 1
    adjusted_matrix = adjusted_matrix.toarray()
    del confidence_matrix
    del book_user_mat_bool_sparse

    # calculate product matrix
    prod = book_user_mat_sparse.dot(book_user_mat_sparse.T)
    prod = prod.toarray()

    # calculate similarity matrix
    similarity_matrix = (prod / weight) * adjusted_matrix
    del prod
    del weight
    del adjusted_matrix

    # replace invalid value with -1
    similarity_matrix[np.isnan(similarity_matrix)] = -1

    # replace similarities larger than 1 with 1
    similarity_matrix[(similarity_matrix > 1) & (similarity_matrix < 1.01)] = 0

    # distance
    distance_matrix = 1 - similarity_matrix

    # define model
    model_knn = NearestNeighbors(metric='precomputed', algorithm='brute', n_neighbors=NUM_NEIGHBORS, n_jobs=-1)
    # fit
    model_knn.fit(distance_matrix)

    distances, indices = model_knn.kneighbors(distance_matrix, n_neighbors=NUM_NEIGHBORS)

    del distance_matrix
    del similarity_matrix

    similarities = 1 - distances
    del distances

    save_arbitrary_obj(similarities, similarities_path)
    save_arbitrary_obj(indices, indices_path)
    save_arbitrary_obj(book_id_to_idx, book_id_to_idx_path)
    save_arbitrary_obj(user_id_to_idx, user_id_to_idx_path)
    save_arbitrary_obj(book_idx_to_id, book_idx_to_id_path)
    save_arbitrary_obj(user_idx_to_id, user_idx_to_id_path)


def train_popularity():
    df_ratings = pd.read_csv(rating_file_path, usecols=['user_id', 'book_id', 'rating'])
    df_books_cnt = pd.DataFrame(df_ratings.groupby('book_id').size(), columns=['num_rating'])
    df_books_cnt['average'] = df_ratings.groupby('book_id').mean()['rating']

    C = df_books_cnt.num_rating.quantile([0.25]).iloc[0]
    m = np.sum(df_books_cnt['average'] * df_books_cnt['num_rating']) / np.sum(df_books_cnt['num_rating'])

    df_books_cnt['bayes_average'] = ((df_books_cnt['average'] * df_books_cnt['num_rating']) + C * m)

    popular_arr = np.array(df_books_cnt.sort_values('bayes_average', ascending=False).index)

    save_arbitrary_obj(popular_arr, popularity_path)


def predict_popularity():
    popular_arr = load_arbitrary_obj(popularity_path)
    return popular_arr.tolist()


def predict(user_id):
    similarities = load_arbitrary_obj(similarities_path)
    indices = load_arbitrary_obj(indices_path)
    book_id_to_idx = load_arbitrary_obj(book_id_to_idx_path)
    book_idx_to_id = load_arbitrary_obj(book_idx_to_id_path)

    ratings_list = get_user_ratings_from_user_id(user_id)
    ratings_list = filter(lambda x: x[0] in book_id_to_idx, ratings_list)
    ratings_list = map(lambda x: (book_id_to_idx[x[0]], x[1]), ratings_list)
    ratings_dict = {}
    for x, y in ratings_list:
        ratings_dict[x] = y

    user_ratings = np.zeros(len(indices))
    for i in range(len(indices)):
        if i in ratings_dict:
            user_ratings[i] = ratings_dict[i]
        else:
            user_ratings[i] = np.nan

    user_ratings_matrix = user_ratings[indices]

    user_has_ratings = ~np.isnan(user_ratings_matrix)
    del user_ratings_matrix

    # filter all simiarity value that has no ratings
    similarities_filtered = similarities * user_has_ratings
    del user_has_ratings

    # get the indexes of top k largest similarity value for each row
    top_k = (-similarities_filtered).argpartition(k, axis=1)[:, :k]

    rows = np.arange(0, similarities.shape[0])[:, None]

    # apply the index to both the simialrities and the indices
    top_k_similarity = similarities_filtered[rows, top_k]
    top_k_similarity[top_k_similarity < 0] = 0

    indices_filtered = indices[rows, top_k]

    invalids_arr = ~(np.sum(top_k_similarity > 0, axis=1) > min_k)

    # predict the rating
    predicted_ratings = np.nansum((user_ratings[indices_filtered] * top_k_similarity), axis=1) / np.sum(
        top_k_similarity, axis=1)

    predicted_ratings[invalids_arr] = np.nan

    result = {}
    for i in range(len(predicted_ratings)):
        if not np.isnan(predicted_ratings[i]):
            result[book_idx_to_id[i]] = predicted_ratings[i]

    return result


class Train(Resource):
    def get(self):
        # download_file_from_postgres()
        train()
        train_popularity()
        return Response(status=200)


class Predict(Resource):
    def get(self, user_id):
        return predict(user_id)


class PredictPopularity(Resource):
    def get(self):
        return predict_popularity()


api.add_resource(Train, '/train')
api.add_resource(Predict, '/predict/<int:user_id>')
api.add_resource(PredictPopularity, '/predict/popular')


if __name__ == '__main__':
    app.run(debug=True)
