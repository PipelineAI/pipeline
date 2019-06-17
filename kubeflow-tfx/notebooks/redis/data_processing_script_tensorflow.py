def pre_process_3ch(image):
    return image.float().div(255).unsqueeze(0)

def pre_process_4ch(image):
    return image.float().div(255)[:,:,:-1].contiguous().unsqueeze(0)

def post_process(output):
    # tf model has 1001 classes, hence negative 1
    return output.max(1)[1] - 1
