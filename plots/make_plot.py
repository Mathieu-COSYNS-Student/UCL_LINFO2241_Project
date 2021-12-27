#!/usr/bin/env python3

import sys
import getopt
import pandas as pd
import matplotlib.pyplot as plt
import numpy as np

GROUP_BY_COLUMN = 'load_name'
VALUES_COLUMN = 'elapsed_time_seconds'


def main(argv):
    options = read_options_from_cli(argv[1:])
    # print(f'options: {options}')

    df = read_csv_and_agg(options['inputs'])

    # print(df.head())

    plot(df,
         type=options['type'],
         title=options['title'],
         means_labels=options['means_labels'],
         xlabel=options['x'],
         ylabel=options['y'],
         pdf=options['output_pdf'],
         png=options['output_png'],
         graphics=options['graphics'])


def read_csv_and_agg(files):
    if not len(files):
        print("No input provided", file=sys.stderr)
        sys.exit(1)

    main_df = None

    for i, file in enumerate(files):
        df = pd.read_csv(file)

        df = df[[GROUP_BY_COLUMN, VALUES_COLUMN]]

        df = df.groupby(GROUP_BY_COLUMN, sort=False).agg([np.mean, np.std])

        df.rename(
            columns={VALUES_COLUMN: f"{VALUES_COLUMN}_{i}"}, inplace=True)

        if main_df is None:
            main_df = df
        else:
            main_df = pd.concat([main_df, df], axis=1)

    return main_df


def plot(df, type=None, title=None, means_labels=None, xlabel=None, ylabel=None, pdf=None, png=None, graphics=True):

    columns = []

    for column in df.columns.values:
        columns.append(column[0])

    columns = list(dict.fromkeys(columns))

    if type == 'bar':
        ax = plt.gca()
        df.columns = ['_'.join(column)
                      for column in df.columns.to_flat_index()]
        means = [s for s in df.columns if s.endswith('mean')]
        stds = [s for s in df.columns if s.endswith('std')]
        df.plot.bar(
            y=means,
            yerr=df[stds].to_numpy().T,
            ecolor='black',
            capsize=5,
            ax=ax,
            rot=0,
            label=means_labels)
        ax.grid(True, axis='y')
        ax.set_axisbelow(True)
    else:
        ax = plt.gca()
        for i, column in enumerate(columns):
            label = None
            if len(means_labels) > i:
                label = means_labels[i]
                df[column].plot(y='mean',
                                yerr='std',
                                ecolor='black',
                                capsize=5,
                                ax=ax,
                                label=label,
                                grid=True)
                plt.xticks(df[columns[0]].index.values.tolist())

    plt.title(title)

    plt.xlabel(xlabel)
    plt.ylabel(ylabel)

    # plt.xlim(left=0)
    plt.ylim(bottom=0)

    if pdf != None:
        plt.savefig(pdf)

    if png != None:
        plt.savefig(png)

    if graphics:
        plt.show()

    plt.close()


def print_usage(*args, **kwargs):
    print(f"""Usage: {sys.argv[0]} [OPTION]...

DESCRIPTION
	Create a report based on the output of test_time.sh.

	-i [input]
		set the name of the input file.

	-m [label], --mean-label=[label]
		label shown in the legend
	
	--no-graphics
		disable the graphical interface

	-o [output]
		'-o output' if the same as setting '--out-pdf output.pdf' and '--out-png output.png'.

	--out-pdf=[output]
		set the name of the pdf output file. Override -o if provided.

	--out-png=[output]
		set the name of the png output file. Override -o if provided.

	-t [title], --title=[title]
		title of the graph

	--type=[type]
	  type of the graph

	-x [label], --xlabel=[label]
		label on x axis
	
	-y [label], --ylabel=[label]
		label on y axis
""", *args, **kwargs)


def read_options_from_cli(args):
    inputs = []
    output_pdf = None
    output_png = None
    graphics = True
    title = None
    type = None
    x = None
    y = None
    means_labels = []

    try:
        opts, args = getopt.getopt(
            args, 'hi:m:o:t:x:y:', ['help', 'mean-label=', 'out-pdf=', 'out-png=', 'no-graphics', 'title=', 'type=', 'xlabel=', 'ylabel='])
    except getopt.GetoptError:
        print_usage(file=sys.stderr)
        sys.exit(1)

    # parse options
    for opt, arg in opts:
        if opt in ('-h', '--help'):
            print_usage()
            sys.exit()
        elif opt in ('-i'):
            inputs.append(arg)
        elif opt in ('-o'):
            if output_pdf == None:
                output_pdf = f'{arg}.pdf'
            if output_png == None:
                output_png = f'{arg}.png'
        elif opt in ('-m', '--mean-label'):
            means_labels.append(arg)
        elif opt in ('--no-graphics'):
            graphics = False
        elif opt in ('--out-pdf'):
            output_pdf = arg
        elif opt in ('--out-png'):
            output_png = arg
        elif opt in ('-t', '--title'):
            title = arg
        elif opt in ('--type'):
            type = arg
        elif opt in ('-x', '--xlabel'):
            x = arg
        elif opt in ('-y', '--ylabel'):
            y = arg

    if input == None:
        print("No input file provided. Use the -i option", file=sys.stderr)
        sys.exit(1)

    return {
        'inputs': inputs,
        'output_pdf': output_pdf,
        'output_png': output_png,
        'graphics': graphics,
        'title': title,
        'type': type,
        'means_labels': means_labels,
        'x': x,
        'y': y
    }


if __name__ == '__main__':
    main(sys.argv)
