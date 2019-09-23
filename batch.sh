EPOCH=$1
IMAGE_DIR=$2

if [ $# != 2 ]; then
    echo "Usage: $1 epoch $2 IMAGE_DIR"
    exit 1
fi

python ./train.py -g 0 --dir $IMAGE_DIR --epoch $EPOCH --depth 0


for i in `seq 1 6`; do
    python ./train.py -g 0 --dir $IMAGE_DIR --gen results/gen$((i-1) --dis results/dis$((i-1)) --optg results/opt_g --optd results/opt_d --epoch $EPOCH --depth $i
done
