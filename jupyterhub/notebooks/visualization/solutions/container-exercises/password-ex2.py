numbers = widgets.Checkbox(description='Include numbers in password')
words = widgets.Label('The generated password is:')
toggles = widgets.ToggleButtons(description='Type of special characters to include',
                                options=[',./;[', '!@#~%', '^&*()'],
                                style={'description_width': 'initial'})
vbox = widgets.VBox()

# The border is set here just to make it easier to see the position of
# the children with respect to the box.
vbox.layout.border = '2px solid grey'
vbox.layout.height = '250px'

# Added lines:
vbox.layout.justify_content = 'space-around'
vbox.layout.align_items = 'flex-start'
# Don't forget to add the children...
vbox.children = [words, toggles, numbers]

vbox
