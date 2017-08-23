def update_line(change):
    new_x, new_y = fourier_series(change['new'])
    line.x = new_x
    line.y = new_y
