import numpy as np
from PIL import Image
import scipy

# ------------- .png to pixels ----------------------------------------------------------------------------------
#
# convert a .png image to a list of pixel values
#   mode = P (8-bit pixels, mapped to any other mode using a color palette)
#
# ------------- .png to pixels ----------------------------------------------------------------------------------
print('''
.png to pixels

      mode = P (8-bit pixels, mapped to any other mode using a color palette)

''')
img_name = 'example5.png'
img = Image.open(img_name).convert('P')
WIDTH, HEIGHT = img.size
data = list(img.getdata()) # convert image data to a list of integers
print('''
img_name: {}
WIDTH: {}
HEIGHT: {}
img.mode: {}
img.size: {}
image.data: {}

'''.format(img_name, WIDTH, HEIGHT, img.mode, img.size, data))

# convert image data list of integer pixel values to 2D list that can be displayed as an image
data = [data[offset:offset+WIDTH] for offset in range(0, WIDTH*HEIGHT, WIDTH)]

# At this point the image's pixels are all in memory and can be accessed
# individually using data[row][col].

print('Display the 2D list of pixel values as an image')
for row in data:
    print(' '.join('{:3}'.format(value) for value in row))


# ------------- pixels to .png ----------------------------------------------------------------------------------
#
# convert a list of pixel values to a .png image
#   mode = P (8-bit pixels, mapped to any other mode using a color palette)
#
# ------------- pixels to .png ----------------------------------------------------------------------------------
print('''



pixels to .png
        mode = P (8-bit pixels, mapped to any other mode using a color palette)

''')
image_name = '7.png'
# mnist standard image format is 28x28
WIDTH = 28
HEIGHT = 28
# image data as a list of integers
# x = '0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 3 18 18 18 126 136 175 26 166 255 247 127 0 0 0 0 0 0 0 0 0 0 0 0 30 36 94 154 170 253 253 253 253 253 225 172 253 242 195 64 0 0 0 0 0 0 0 0 0 0 0 49 238 253 253 253 253 253 253 253 253 251 93 82 82 56 39 0 0 0 0 0 0 0 0 0 0 0 0 18 219 253 253 253 253 253 198 182 247 241 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 80 156 107 253 253 205 11 0 43 154 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 14 1 154 253 90 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 139 253 190 2 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 11 190 253 70 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 35 241 225 160 108 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 81 240 253 253 119 25 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 45 186 253 253 150 27 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 16 93 252 253 187 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 249 253 249 64 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 46 130 183 253 253 207 2 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 39 148 229 253 253 253 250 182 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 24 114 221 253 253 253 253 201 78 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 23 66 213 253 253 253 253 198 81 2 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 18 171 219 253 253 253 253 195 80 9 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 55 172 226 253 253 253 253 244 133 11 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 136 253 253 253 212 135 132 16 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0'
# image_data = list(map(int, x.split()))
image_data = [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,10,150,238,254,255,237,150,150,225,161,221,203,18,0,0,0,0,0,0,0,0,0,0,0,0,0,0,66,146,253,253,253,253,253,253,253,253,253,253,253,173,0,0,0,0,0,0,0,0,0,0,0,0,0,140,251,253,253,178,114,114,114,114,114,114,167,253,253,154,0,0,0,0,0,0,0,0,0,0,0,12,84,240,253,248,170,28,0,0,0,0,0,0,90,253,250,90,0,0,0,0,0,0,0,0,0,0,10,129,226,253,235,128,0,0,0,0,0,0,0,8,188,253,190,0,0,0,0,0,0,0,0,0,0,0,56,250,253,246,98,0,0,0,0,0,0,0,0,76,243,234,100,0,0,0,0,0,0,0,0,0,0,0,185,253,248,44,0,0,0,0,0,0,0,0,34,245,253,95,0,0,0,0,0,0,0,0,0,0,0,0,69,187,87,0,0,0,0,0,0,0,0,22,164,253,223,63,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,55,247,253,85,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,47,230,253,184,10,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,145,253,241,43,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,57,250,253,206,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,135,253,253,40,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,105,251,248,108,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,68,226,253,180,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,40,233,253,205,13,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,5,189,253,240,72,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,110,253,253,109,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,47,242,253,159,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,43,198,228,24,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]
json_data = [image_data[offset:offset+WIDTH] for offset in range(0, WIDTH*HEIGHT, WIDTH)]
image = Image.fromarray(np.asarray(json_data, dtype=np.uint32))
image.show()
# json_data = []

# mode = P (8-bit pixels, mapped to any other mode using a color palette)
image_mode = 'P'
image_size = (WIDTH, HEIGHT)
print('''
image_name: {}
WIDTH: {}
HEIGHT: {}
img.mode: {}
img.size: {}
image.data: {}

'''.format(image_name, WIDTH, HEIGHT, image_mode, image_size, image_data))

# image_out = Image.new(image_mode, image_size)
# image_out.putdata(json_data)
# image_out.save(image_name)

# json_data = [json_data[offset:offset+WIDTH] for offset in range(0, WIDTH*HEIGHT, WIDTH)]

print('Display the 2D list of pixel values as an image')
for row in json_data:
    print(' '.join('{:3}'.format(value) for value in row))
