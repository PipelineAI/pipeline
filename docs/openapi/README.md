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
          '*/*':
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
            '*/*':
              schema:
                type: object
        '500':
          description: Internal Error
          content:
            '*/*':
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
            '*/*':
              schema:
                type: object
        '500':
          description: Internal Error
          content:
            '*/*':
              schema:
                type: object
```

### AWS SageMaker InvokeEndpoint API

Derived from https://docs.aws.amazon.com/sagemaker/latest/dg/your-algorithms-inference-code.html

```
openapi: 3.0.0
info:
  title: AWS SageMaker Prediction API
  version: 1.0.0
  description: AWS SageMaker Prediction API
paths:
  /endpoints/{endpoint-name}/invocations:
    parameters:
    - name: endpoint-name
      in: path
      required: true
      description: Endpoint Name
      schema:
        type: string
    post:
      requestBody:
        content:
          '*/*':
            schema:
              type: object
      responses:
        '200':
          description: Success
        '500':
          description: InternalFailure
          content:
            '*/*':
              schema:
                type: object
        '424':
          description: ModelError
        '503':
          description: ServiceUnavailable
        '400':
          description: ValidationError
  /ping:
    get:
      responses:
        '200':
          description: Success
```
Generated with http://editor.swagger.io/
