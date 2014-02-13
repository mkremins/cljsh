./bin/cljsc ./src/cljs/app.cljs > ./js/app.js
rm -rf ./js/out
mv ./out ./js
