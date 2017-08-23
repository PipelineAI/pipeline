# This example links two selection widgets

options = ['yes', 'no', 'maybe']

drop = widgets.Dropdown(options=options)
radio = widgets.RadioButtons(options=options)
widgets.jslink((drop, 'index'),  (radio, 'index'))
display(drop, radio)
