package cz.cvut.fit.ostrajava.Interpreter.Natives.IO.Console;

import cz.cvut.fit.ostrajava.Interpreter.InterpreterException;
import cz.cvut.fit.ostrajava.Interpreter.Memory.Array;
import cz.cvut.fit.ostrajava.Interpreter.Memory.Heap;
import cz.cvut.fit.ostrajava.Interpreter.Memory.HeapOverflow;
import cz.cvut.fit.ostrajava.Interpreter.Natives.Native;
import cz.cvut.fit.ostrajava.Interpreter.StackValue;

import java.util.Scanner;

public class NextLine extends Native
{
	private final Scanner scanner = new Scanner(System.in);

	public NextLine (Heap heap)
	{
		super(heap);
	}

	@Override
	//Arguments: void (none)
	public StackValue invoke (StackValue[] args) throws InterpreterException, HeapOverflow
	{
		String input = scanner.nextLine();

		//Create array of chars
		StackValue reference = null;

		reference = heap.allocArray(input.length());
		Array charArray = heap.loadArray(reference);

		for (int i = 0; i < input.length(); i++)
		{
			StackValue charValue = new StackValue(input.charAt(i), StackValue.Type.Primitive);
			charArray.set(i, charValue);
		}

		return reference;
	}
}