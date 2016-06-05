import matplotlib.pyplot as plt
from math import sin, cos
from glob import glob
import re
import sys

avg = glob('*avg*csv')
client = glob('*client*csv')
request = glob('*request*csv')

cnt = 1

# metrics: avg, client, request
# parameters: N, M, D
def draw_by_list(files, metric, parameter):
    global cnt
    plt.figure(cnt)
    output_name = metric + parameter + '.png'
    cnt += 1
    for entry in files:
        x_axis = []
        y_axis = []
        not_first = False
        with open(entry) as csv_file:
            for line in csv_file:
                if not_first:
                    els = list(map(int, line.split(', ')))
                    x_axis.append(els[1])
                    y_axis.append(els[0])
                else:
                    not_first = True
        res = re.match('results(.*)' + metric + '.*', entry)
        server_name = res.group(1)
        plt.plot(x_axis, y_axis, label=server_name)
    plt.legend()
    plt.xlabel(parameter)
    plt.ylabel(metric)    
    plt.savefig(output_name)
        

if __name__ == '__main__':
    draw_by_list(avg, 'avg', sys.argv[1])
    draw_by_list(client, 'client', sys.argv[1])
    draw_by_list(request, 'request', sys.argv[1])
    plt.show()
