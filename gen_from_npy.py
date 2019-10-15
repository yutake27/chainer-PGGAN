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


def get_args():
    parser = argparse.ArgumentParser()
    parser.add_argument('--gpu', '-g', type=int, default=-1)
    parser.add_argument('--gen', type=str, default="predicted_model", help='input generate model path')
    parser.add_argument('--depth', '-d', type=int, default=6)
    parser.add_argument('--out', '-o', type=str, default='out/out.png')
    parser.add_argument('--seed', '-s', type=int, default=255)
    parser.add_argument('--size', '-si', type=int, default=1, choices=[0,1,2])
    parser.add_argument('--structure', '-st', type=int, default=1, choices=[0,1,2])
    parser.add_argument('--color', '-c', type=int, default=2, choices=[0,1,2])
    args = parser.parse_args()
    return args


def generate(z, args):

    gen = network.Generator(depth=args.depth)
    print('loading generator model from ' + args.gen)
    serializers.load_npz(args.gen, gen)

    if args.gpu >= 0:
        cuda.get_device_from_id(0).use()
        gen.to_gpu()

    xp = gen.xp

    x = gen(z, alpha=1.0)
    x = chainer.cuda.to_cpu(x.data)
    img = x[0].copy()
    out_file = Path(args.out)
    utils.save_image(img, out_file)
    np.save(out_file.with_suffix('.npy'), z)
    print(out_file)


def make(args):
        size_dic = {0:'s', 1:'m', 2:'b'}
        st_dic = {0:'a', 1:'m', 2:'b'}
        color_dic = {0:'g', 1:'gb', 2:'c'}
        size = size_dic[args.size]
        st = st_dic[args.structure]
        color = color_dic[args.color]
        
        np.random.seed(args.seed)
        array = []
        out_path = Path('./out')
        with open('./pggan.txt', 'r') as f:
            lines = f.readlines()
            for line in lines[1:]:
                n, l_size, l_st, l_color = line.split()
                if size==l_size and st==l_st and color==l_color:
                    npy = np.load(out_path/'gen{}.npy'.format(n))
                    array.append(npy)
            if len(array)==0:
                for line in lines[1:]:
                    n, l_size, l_st, l_color = line.split()
                    if size==l_size and (st==l_st or color==l_color):
                        npy = np.load(out_path/'gen{}.npy'.format(n))
                        array.append(npy)
            if len(array)==0:
                print('omg')
                for line in lines[1:]:
                    n, l_size, l_st, l_color = line.split()
                    if size==l_size:
                        npy = np.load(out_path/'gen{}.npy'.format(n))
                        array.append(npy)
        array = np.array(array)
        array = array[np.random.choice(len(array), 3, replace=True)]
        z = np.mean(array, axis=0)
        generate(z, args)

if __name__ == '__main__':
    args = get_args()
    make(args)