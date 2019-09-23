# PGGAN

clone from https://github.com/joisino/chainer-PGGAN
and add support for multi gpu with chainermn

### Progressive Growing of GANs implemented with chainer

 `python 3.6.8`

 `chainer 6.0.0`

 `cuda 9.2.148`

 `openmpi 2.1.2-opa10.9`

 `nccl 2.4.2`


## Usage

### Training

```
python ./train.py -g 0 --dir ./train_images/ --epoch 100 --depth 0
```

When `depth = n`, generated images are `2^{n+2} x 2^{n+2}` size.

If you have multi gpu,
```
mpiexec -n GPU_NUM ./train.py -g 0 --dir ./train_images/ --epoch 100 --depth 0
```

Please put the number of gpu in `NUM_GPU`.

```
$ ./batch.sh NUM_GPU 100 ./train_images
```

`batch.sh` automatically trains models gradually (through `4 x 4` to `256 x 256`).


You should tune `delta` and `epoch` when it changes too quickly or too slowly.

### Generating

```
$ python ./generate.py --gen GEN_MODEL_PATH --depth 0
```


## Bibliography

[1] http://research.nvidia.com/publication/2017-10_Progressive-Growing-of

The original paper

[2] https://github.com/dhgrs/chainer-WGAN-GP

WGAN-GP implemented with chainer.

[3] https://github.com/joisino/chainer-PGGAN

PGGAN implementated with chainer.
