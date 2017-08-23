def calculate_password(change):
    import string
    from secrets import choice
    length = change.new
    # Generate a list of random letters of the correct length.
    password = ''.join(choice(string.ascii_letters) for _ in range(length))
    # Add a line below to set the value of the widget password_text
    password_text.value = password
