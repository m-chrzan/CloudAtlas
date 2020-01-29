for num in 02 03 04 05; do
	scp build/tmp/query_signer.pub "rainbow${num}:~/CloudAtlas/build/tmp/"
done
