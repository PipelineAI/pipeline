Deploy a PipelineIO model.

### Usage
```bash
pio deploy 
```

### (Optional) Parameters 
| Name, shorthand | Default | Description |
| --------------- | ------- | ----------- |
|                 |         |             |

### Description
PipelineIO tracks models based on the location of your code. 
This command initializes a new project at the current directory and tracks all files and subdirectories. 
These files will be uploaded when you deploy your model to the PipelineIO Servers. 

### Initialize Model
You must use `pio init-model` before deploying the model.  See the PipelineIO CLI command [init-model](init-model.md) for more details.

### Examples
Deploy a PipelineIO Model
```bash
$ pio init-model ... 
$ pio deploy
```

{!contributing.md!}
