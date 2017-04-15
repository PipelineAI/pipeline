## Flow example
 
This directory contains a simple function and flow.

To install them run:

    funktion apply -f examples/flow -w
    
This will create a Function and a Flow resource and watch the files for changes and update them on the fly.

The [sample.flow.yml](sample.flow.yml) defines a simple Flow of events which then invokes the [hello.js](hello.js) function