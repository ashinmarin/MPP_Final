import os
import sys
import json
import matplotlib
matplotlib.use('TkAgg')
import matplotlib.pyplot as plt

if __name__ == '__main__':
    ##################################################################
    # global data
    ##################################################################
    list_names = ['Coarse',
                  'Fine',
                  'Lazy',
                  'LockFree',
                  'Optimistic',
                  'EliminationBackoff',
                  'EliminationCombining'
                  ]

    labels = {
        'Optimistic': 'r^-',
        'Fine': 'bs-',
        'Lazy': 'go-',
        'Coarse': 'yv-',
        'LockFree': 'mX-',
        'EliminationBackoff': 'kd-',
        'EliminationCombining': 'c8-'
    } 

    thread_nums = [num for num in range(4, 40, 4)]
    
    contains_nums = [num for num in range(20, 100, 20)]
    symmetric_nums = [(100 - num)//2 for num in contains_nums]
    add_nums = [num for num in range(20, 100, 20)]
    rm_nums = [100 - num for num in add_nums]

    symmetric_res_dir = os.path.join(os.getcwd(), 'res', 'symmetric')
    asymmetric_res_dir = os.path.join(os.getcwd(), 'res', 'asymmetric')
    plot_res_dir = os.path.join(os.getcwd(), 'res', 'plot_res')
    if not os.path.exists(plot_res_dir):
        os.makedirs(plot_res_dir)

    ##################################################################
    # symmetric plot
    ##################################################################
    for directory in [symmetric_res_dir, asymmetric_res_dir]:
        for filename in [name for name in os.listdir(directory) if name.endswith('.txt')]:
            file_data = {}
            file_path = os.path.join(directory, filename)
            with open(file_path, 'r') as f:
                file_data = json.loads(f.read())
            fig = plt.figure()
            ax = plt.subplot(111)
            for list_name, thread_data in file_data.items():
                # for list_name, thread_data in data.items():
                ax.plot(thread_nums, thread_data, labels[list_name], label=list_name+'List')
                plt.ylabel('Throughput (opt/ms)')
                # plt.title('threads | contains {}%'.format(contains))
                ax.legend()
                plt.xlabel('Thread #')
                # plt.show()
            fig.savefig('{}'.format(os.path.join(plot_res_dir, filename.split('.')[0]), dpi=fig.dpi))
