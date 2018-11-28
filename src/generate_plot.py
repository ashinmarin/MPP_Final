import os
import sys
import json
import matplotlib
matplotlib.use('TkAgg')
import matplotlib.pyplot as plt


if __name__ == '__main__':
    threads = [i for i in range(4, 42, 2)]
    data_1_4_path = os.path.join(os.getcwd(), 'res', 'data_1_4.txt')
    labels = {
        'Optimistic': 'r^-',
        'Fine': 'bs-',
        'Lazy': 'go-',
        'Coarse': 'yv-',
        'LockFree': 'mX-',
        'EliminationBackoff': 'kd-',
        'EliminationCombining': 'c8-'
    } 
    with open(data_1_4_path, 'r') as f:
        data_1_4 = json.loads(f.read())
    for contains, data in data_1_4.items():
        fig = plt.figure()
        ax = plt.subplot(111)
        for list_name, thread_data in data.items():
            ax.plot(threads, thread_data, labels[list_name], label=list_name+'List')
            # print(list_name)
        plt.ylabel('Throughput (opt/ms)')
        plt.title('throughput vs. threads | contains {}%'.format(contains))
        ax.legend()
        plt.xlabel('Thread #')
        fig.savefig('{}'.format(os.path.join(os.getcwd(), 'res', contains), dpi=fig.dpi))

    data_5_path = os.path.join(os.getcwd(), 'res', 'data_5.txt')
    with open(data_5_path, 'r') as f:
        data_5 = json.loads(f.read())

    fig = plt.figure()
    ax = plt.subplot(111)

    for list_name, contains in data_5.items():
        ax.plot([20, 40, 60, 80], contains, labels[list_name], label=list_name+'List')
    plt.xlabel('Contains (%)')
    plt.title('throughput vs. contains | thread 20')
    ax.legend()
    plt.ylabel('Throughput (opt/ms)')
    fig.savefig('{}'.format(os.path.join(os.getcwd(), 'res', 'fixed_thread'), dpi=fig.dpi))


    # ys = [i for i in range(4, 42, 2)]
    # ys2 = [i**2 for i in range(4, 42, 2)]
    # ax.plot(threads, ys2, label='$y2 = other numbers')
    # plt.title('Legend inside')
    # ax.legend(loc='upper center', bbox_to_anchor=(0.5, -0.05),  shadow=True, ncol=2)
    # plt.show()
    # plt.plot(threads, ys, 'rs-')
    # plt.show()
    

