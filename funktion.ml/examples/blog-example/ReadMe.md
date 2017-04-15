## Blog Example

This example shows how to combine some functions and flows.

Here's how to try it out. For more help on how to use the funktion CLI [check out the documentation](https://funktion.fabric8.io/docs/#cli)

```json

echo "lets create the functions - pass -w to watch for file changes"
funktion create fn -f examples/blog-example

echo  "lets create a flow"
funktion create flow -n blogendpoint -c http4 http://localhost http://blogcount

echo  "lets invoke the funky flow"
export SPLIT=`minikube service --url split -n funky`
echo $SPLIT

curl -X POST --header "Content-Type: application/json"  -d '
[
  {
    "userId": 1,
    "id": 1,
    "title": "sunt aut facere repellat provident occaecati excepturi optio reprehenderit",
    "body": "quia et suscipit\nsuscipit recusandae consequuntur expedita et cum\nreprehenderit molestiae ut ut quas totam\nnostrum rerum est autem sunt rem eveniet architecto"
  },
  {
    "userId": 1,
    "id": 2,
    "title": "qui est esse",
    "body": "est rerum tempore vitae\nsequi sint nihil reprehenderit dolor beatae ea dolores neque\nfugiat blanditiis voluptate porro vel nihil molestiae ut reiciendis\nqui aperiam non debitis possimus qui neque nisi nulla"
  }
]
' $SPLIT
```

