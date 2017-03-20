import pickle

# Load the pickled model
decision_tree_pkl_filename = 'store/default/python_balancescale/1/python_balancescale.pkl'
 
decision_tree_model_pkl = open(decision_tree_pkl_filename, 'rb')
decision_tree_model = pickle.load(decision_tree_model_pkl)

print("prediction: ", decision_tree_model.predict([[1,1,3,4]]))

# Close the pickled model file reference
decision_tree_model_pkl.close()
