import pickle

# Dump the trained decision tree classifier with Pickle
simple_pkl_filename = 'models/python_simple/python_simple.pkl'

# Open the file to save as pkl file
simple_pkl = open(simple_pkl_filename, 'wb')
pickle.dump([1,2], simple_pkl)

# Close the pickle instances
simple_pkl.close()

# Loading the saved decision tree model pickle
simple_pkl = open(simple_pkl_filename, 'rb')
simple = pickle.load(simple_pkl)

print(simple)
