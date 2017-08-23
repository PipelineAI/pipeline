helpful_title = widgets.HTML('Generated password is:')
password_text = widgets.HTML('No password yet', placeholder='No password generated yet')
password_text.layout.margin = '0 0 0 20px'
password_length = widgets.IntSlider(description='Length of password',
                                   min=8, max=20,
                                   style={'description_width': 'initial'})

password_widget = widgets.VBox(children=[helpful_title, password_text, password_length])
password_widget
