import re

# vendored from the 'requests' module
def parse_header_links(value):
    """Return a dict of parsed link headers proxies.

    i.e. Link: <http:/.../front.jpeg>; rel=front; type="image/jpeg",<http://.../back.jpeg>; rel=back;type="image/jpeg"

    :rtype: list
    """

    links = []

    replace_chars = ' \'"'

    for val in re.split(', *<', value):
        try:
            url, params = val.split(';', 1)
        except ValueError:
            url, params = val, ''

        link = {'url': url.strip('<> \'"')}

        for param in params.split(';'):
            try:
                key, value = param.split('=')
            except ValueError:
                break

            link[key.strip(replace_chars)] = value.strip(replace_chars)

        links.append(link)

    return links


def next_page_from_links(response):
    """Return the url of the 'next' link header as a string.

    Return None if no link header called 'next' is found.

    Both Gitlab and Github use link headers for pagination:
        https://docs.gitlab.com/ee/api/README.html#pagination-link-header
        https://developer.github.com/v3/#pagination
    """
    link_header = response.headers.get('Link')
    if not link_header:
        return None

    for link in parse_header_links(link_header):
        if link.get('rel') == 'next':
            return link['url']
    # if no "next" link, this page is the last one
    return None
