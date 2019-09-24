import numpy as np
import argparse
import os
from PIL import Image
import chainer
from chainer import serializers
from chainer import Variable
from chainer import cuda
import dataset
import network
import utils
from pathlib import Path

def generate():
    parser = argparse.ArgumentParser()
    parser.add_argument('--gpu', '-g', type=int, default=-1)
    parser.add_argument('--gen', type=str, default=None, help='input generate model path')
    parser.add_argument('--depth', '-d', type=int, default=0)
    parser.add_argument('--out', '-o', type=str, default='gen_img')
    parser.add_argument('--num', '-n', type=int, default=10)
    parser.add_argument('--vector_ope', '-v', nargs=2, type=str, help='input 2 npz path')
    args = parser.parse_args()

    gen = network.Generator(depth=args.depth)
    print('loading generator model from ' + args.gen)
    serializers.load_npz(args.gen, gen)

    out_dir = Path(args.out)
    out_dir.mkdir(exist_ok=True)

    if args.gpu >= 0:
        cuda.get_device_from_id(0).use()
        gen.to_gpu()

    xp = gen.xp

    if args.vector_ope:
        print('load vector from {}'.format(args.vector_ope))
        z1 = np.load(args.vector_ope[0])
        z2 = np.load(args.vector_ope[1])
    else:
        z1 = gen.z(1)
        z2 = gen.z(1)

    for i in range(args.num):
        print(i)
        p = i / (args.num-1)
        z = z1*(1-p) + z2*p
        x = gen(z, alpha=1.0)
        x = chainer.cuda.to_cpu(x.data)

        img = x[0].copy()
        filename = out_dir/'gen_{:04d}.png'.format(i)
        utils.save_image(img, filename)
        np.save(filename.with_suffix('.npy'), z)

if __name__ == '__main__':
    generate()
