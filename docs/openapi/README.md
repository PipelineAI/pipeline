### PipelineAI Predict API

```
openapi: 3.0.0
info:
  title: PipelineAI Prediction API
  version: 1.0.0
  description: PipelineAI Prediction API
paths:
  /:
    post:
      requestBody:
        description: Prediction Inputs
        content:
          application/json:
            schema:
              type: object
      responses:
        '200':
          description: Success
          content: 
            application/json:
              schema:
                properties:
                  outputs:
                    type: object
        '429':
          description: Resource Exhausted
          content: 
            application/json:
              schema:
                type: string
        '500':
          description: Internal Error
          content: 
            application/json:
              schema:
                type: object
  /ping:
    get:
      responses:
        '200':
          description: Success
          content: 
            application/json:
              schema:
                properties:
                  status:
                    type: string
        '429':
          description: Resource Exhausted
          content: 
            application/json:
              schema:
                type: string
        '500':
          description: Internal Error
          content: 
            application/json:
              schema:
                type: object
```
