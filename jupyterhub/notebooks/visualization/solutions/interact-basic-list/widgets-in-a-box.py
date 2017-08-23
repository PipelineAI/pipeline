application = widgets.Textarea(description='Fill this box')
b = widgets.Button(description='Click me')
v = widgets.Valid(description='The text is')

vbox = widgets.VBox(children=[application, b, v])
vbox
