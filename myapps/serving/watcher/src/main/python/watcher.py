'''
Observer pattern lab
"Lab: Watching a File Change Size"

From Aaron Maxwell's awesome class "Python: Beyond Basics"

In production, should use something like this:
  https://pypi.python.org/pypi/inotify
'''

import os
import time

class FileWatcher(object):
    def __init__(self, path_of_file_to_watch):
        self.path = path_of_file_to_watch
        self.observers_for_channel = {
            'increase': set(),
            'decrease': set(),
            }
        self._last_size = 0

    def register(self, channel, observer):
        self.observers_for_channel[channel].add(observer)
    def unregister(self, channel, observer):
        self.observers_for_channel[channel].discard(observer)

    def check_forever(self):
        while True:
            self.check_file()
            time.sleep(0.1)

    def check_file(self):
        size = os.stat(self.path).st_size
        if size != self._last_size:
            if size > self._last_size:
                channel = 'increase'
            else:
                channel = 'decrease'
            self._last_size = size
            self.dispatch(channel, size)

    def dispatch(self, channel, size):
        for observer in self.observers_for_channel[channel]:
            observer.update(size)

class FileObserver(object):
    def __init__(self, name):
        self.name = name
    def update(self, size):
        print('{} noticed that the file is now {} bytes'.format(self.name, size))

bob = FileObserver('Bob')
john = FileObserver('John')
stacy = FileObserver('Stacy')

watcher = FileWatcher('data/watched.txt')
watcher.register('increase', bob)
watcher.register('decrease', john)
watcher.register('increase', stacy)
watcher.register('decrease', stacy)

watcher.check_forever()

