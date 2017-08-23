from ipywidgets import widgets
from model import PassGen
import traitlets

class PassGenGUI(widgets.VBox):
    def __init__(self):
        super(PassGenGUI, self).__init__()

        self._helpful_title = widgets.HTML('Generated password is:')
        self._password_text = widgets.HTML('', placeholder='No password generated yet')
        self._password_text.layout.margin = '0 0 0 20px'
        self._password_length = widgets.IntSlider(description='Length of password',
                                                  min=8, max=20,
                                                  style={'description_width': 'initial'})

        self._numbers = widgets.Checkbox(description='Include numbers in password',
                          style={'description_width': 'initial'})

        self._label = widgets.Label('Choose special characters to include')
        self._toggles = widgets.ToggleButtons(description='',
                                options=[',./;[', '!@#~%', '^&*()'],
                                style={'description_width': 'initial'})

        # Set the margins to control the indentation.
        # The order is top right bottom left
        self._toggles.layout.margin = '0 0 0 20px'

        children = [self._helpful_title, self._password_text, self._password_length,
                    self._label, self._toggles, self._numbers]
        self.children = children

        self.model = PassGen()

        traitlets.link((self.model, 'password'), (self._password_text, 'value'))
        traitlets.link((self.model, 'length'), (self._password_length, 'value'))
        traitlets.link((self.model, 'special_character_groups'), (self._toggles, 'value'))
        traitlets.link((self.model, 'include_numbers'), (self._numbers, 'value'))

    @property
    def value(self):
        return self._password_text.value
