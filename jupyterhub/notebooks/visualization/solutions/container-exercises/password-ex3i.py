vbox_left_layout = widgets.Layout(align_items='flex-start')

label = widgets.Label('Choose special characters to include')
toggles = widgets.ToggleButtons(description='',
                                options=[',./;[', '!@#~%', '^&*()'],
                                style={'description_width': 'initial'})

# Set the margins to control the indentation.
# The order is top right bottom left
toggles.layout.margin = '0 0 0 20px'

better_toggles = widgets.VBox([label, toggles])
better_toggles.layout = vbox_left_layout
better_toggles
