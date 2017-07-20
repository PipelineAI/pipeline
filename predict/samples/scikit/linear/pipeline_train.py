if __name__ == '__main__':
    import numpy as np
    from sklearn import linear_model
    from sklearn import datasets

    # Load the diabetes dataset
    diabetes = datasets.load_diabetes()

    # ONLY USING 1 FEATURE FOR THIS EXAMPLE!
    # Use only one feature
    diabetes_X = diabetes.data[:, np.newaxis, 2]

    # Split the data into training/testing sets
    diabetes_X_train = diabetes_X[:-20]
    diabetes_X_test = diabetes_X[-20:]

    # Split the targets into training/testing sets
    diabetes_y_train = diabetes.target[:-20]
    diabetes_y_test = diabetes.target[-20:]

    # Create linear regression model
    model = linear_model.LinearRegression()

    # Train the model using the training sets
    model.fit(diabetes_X_train, diabetes_y_train)

    import cloudpickle as pickle

    model_pkl_path = 'model.pkl'

    with open(model_pkl_path, 'wb') as fh:
        pickle.dump(model, fh)
