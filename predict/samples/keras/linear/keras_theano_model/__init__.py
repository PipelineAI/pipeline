from keras.models import load_model

class KerasTheanoModel(object):

   def __init__(self,
                model_state_filename):
       self.model = load_model(model_state_filename) 
   
   def predict(self, 
               request: bytes) -> bytes:
       return self.model.predict(request)
