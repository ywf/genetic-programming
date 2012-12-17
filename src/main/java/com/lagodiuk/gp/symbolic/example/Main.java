package com.lagodiuk.gp.symbolic.example;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.lagodiuk.gp.symbolic.SymbolicRegressionEngine;
import com.lagodiuk.gp.symbolic.SymbolicRegressionIterationListener;
import com.lagodiuk.gp.symbolic.TabulatedFunctionFitness;
import com.lagodiuk.gp.symbolic.Target;
import com.lagodiuk.gp.symbolic.interpreter.Expression;
import com.lagodiuk.gp.symbolic.interpreter.Function;
import com.lagodiuk.gp.symbolic.interpreter.Functions;

public class Main {

	private static FileInputStream fileIn;

	private static PrintWriter fileOut;

	public static void main(String[] args) throws Exception {
		configureInputOutput(args);
		BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));

		List<Function> functions = getFunctions(inputReader);
		List<String> variables = getVariables(inputReader);
		TabulatedFunctionFitness fitness = getTrainingData(inputReader, variables);

		SymbolicRegressionEngine engine = new SymbolicRegressionEngine(fitness, variables, functions);

		final String prefix = makePrefix(variables);
		engine.addIterationListener(new SymbolicRegressionIterationListener() {
			private double prevFitValue = -1;

			@Override
			public void update(SymbolicRegressionEngine engine) {
				Expression bestSyntaxTree = engine.getBestSyntaxTree();
				double currFitValue = engine.fitness(bestSyntaxTree);
				if (Double.compare(currFitValue, this.prevFitValue) != 0) {
					outPrintln();
					outPrintln(prefix + bestSyntaxTree.print());
				}

				outPrintln(String.format("%s \t %s", engine.getIteration(), currFitValue));
				this.prevFitValue = currFitValue;
				if (currFitValue < 10) {
					engine.terminate();
				}
			}
		});

		outPrintln();
		outPrintln(String.format("Start time is: %s", new Date()));

		engine.evolve(200);

		outPrintln();
		outPrintln("Best function is:");
		outPrintln(prefix + engine.getBestSyntaxTree().print());
		outPrintln();
		outPrintln(String.format("End time is: %s", new Date()));
		outPrintln();

		closeInOut();
	}

	private static String makePrefix(List<String> variables) {
		String vars = variables.toString();
		vars = vars.substring(1, vars.length() - 1);
		return String.format("f(%s) = ", vars);
	}

	private static List<String> getVariables(BufferedReader inputReader) throws Exception {
		List<String> variables = new ArrayList<String>();
		String s = inputReader.readLine();
		while ((s.startsWith("#")) || (s.trim().isEmpty())) {
			s = inputReader.readLine();
		}
		for (String variableName : s.split("\\s+")) {
			variables.add(variableName);
		}
		return variables;
	}

	private static List<Function> getFunctions(BufferedReader inputReader) throws Exception {
		Set<Function> functions = new HashSet<Function>();
		functions.add(Functions.CONSTANT);
		functions.add(Functions.VARIABLE);
		String s = inputReader.readLine();
		while ((s.startsWith("#")) || (s.trim().isEmpty())) {
			s = inputReader.readLine();
		}
		for (String functionName : s.split("\\s+")) {
			Function f = Functions.valueOf(functionName);
			functions.add(f);
		}
		List<Function> functionsList = new ArrayList<Function>(functions);
		return functionsList;
	}

	private static TabulatedFunctionFitness getTrainingData(
			BufferedReader inputReader, List<String> variables) throws Exception {
		List<Target> targets = new LinkedList<Target>();
		String s = inputReader.readLine();
		while ((s.startsWith("#")) || (s.trim().isEmpty())) {
			s = inputReader.readLine();
		}
		int variablesCount = variables.size();
		while (s != null) {
			if ((s.startsWith("#")) || (s.trim().isEmpty())) {
				s = inputReader.readLine();
				continue;
			}

			String[] values = s.split("\\s+");
			Target target = new Target();
			for (int i = 0; i < variablesCount; i++) {
				double value = Double.parseDouble(values[i]);
				target.when(variables.get(i), value);
			}
			double value = Double.parseDouble(values[variablesCount]);
			target.targetIs(value);
			targets.add(target);

			s = inputReader.readLine();
		}
		return new TabulatedFunctionFitness(targets);
	}

	private static void configureInputOutput(String[] args) throws FileNotFoundException {
		switch (args.length) {
			case 1:
				fileIn = new FileInputStream(args[0]);
				System.setIn(fileIn);
				break;

			case 2:
				System.setIn(new FileInputStream(args[0]));
				fileOut = new PrintWriter(args[1]);
				break;
		}
	}

	private static void outPrintln() {
		System.out.println();
		if (fileOut != null) {
			fileOut.println();
		}
	}

	private static void outPrintln(String message) {
		System.out.println(message);
		if (fileOut != null) {
			fileOut.println(message);
		}
	}

	private static void closeInOut() throws Exception {
		if (fileIn != null) {
			fileIn.close();
		}
		if (fileOut != null) {
			fileOut.close();
		}
	}

}