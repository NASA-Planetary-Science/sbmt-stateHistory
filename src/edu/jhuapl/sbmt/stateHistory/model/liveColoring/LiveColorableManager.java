package edu.jhuapl.sbmt.stateHistory.model.liveColoring;

import java.util.HashMap;

import vtk.vtkFloatArray;

import edu.jhuapl.saavtk.util.NativeLibraryLoader;
import edu.jhuapl.sbmt.image.model.PerspectiveFootprint;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.ICalculatedPlateValues;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.IFootprintConfinedPlateValues;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.ITimeCalculatedPlateValues;
import edu.jhuapl.sbmt.stateHistory.model.time.StateHistoryTimeModel;

public class LiveColorableManager
{
	private static HashMap<String, ICalculatedPlateValues> plateValueCalculators = new HashMap<String, ICalculatedPlateValues>();
	public static StateHistoryTimeModel timeModel = StateHistoryTimeModel.getInstance();

	public static void addICalculatedPlateValues(String name, ICalculatedPlateValues plateValueCalculator)
	{
		plateValueCalculators.put(name, plateValueCalculator);
	}

	public static ICalculatedPlateValues getCalculatedPlateValuesFor(String name)
	{
		return plateValueCalculators.get(name);
	}

	public static void updateFootprint(PerspectiveFootprint footprint)
	{
		for (String name : plateValueCalculators.keySet())
		{
			ICalculatedPlateValues plateValueCalculator = plateValueCalculators.get(name);
			if (plateValueCalculator instanceof IFootprintConfinedPlateValues)
			{
				((IFootprintConfinedPlateValues)plateValueCalculator).setFacetColoringDataForFootprint(footprint);
			}
		}
	}

	public static void main(String[] args)
	{
		NativeLibraryLoader.loadVtkLibraries();

		vtkFloatArray values = new vtkFloatArray();
		values.SetNumberOfComponents(1);

		values.SetNumberOfTuples(10);

		LiveColorableManager.addICalculatedPlateValues("test", new ITimeCalculatedPlateValues()
		{

			@Override
			public vtkFloatArray getValues()
			{
				return values;
			}

			@Override
			public vtkFloatArray getPlateValuesForTime(double time)
			{
				values.InsertTuple1((int)time, 100+time);
				return values;
			}

			@Override
			public void setNumberOfDimensions(int numDimensions)
			{
				values.SetNumberOfComponents(numDimensions);
			}

			@Override
			public void setNumberOfValues(int numValues)
			{
				values.SetNumberOfTuples(numValues);
			}
		});

		ITimeCalculatedPlateValues testCalculator = (ITimeCalculatedPlateValues)LiveColorableManager.getCalculatedPlateValuesFor("test");

		for (int i=0; i<10; i++)
		{
			testCalculator.getPlateValuesForTime(i);
		}

		for (int i=0; i<10; i++)
		{
			System.out.println("LiveColorableManager: main: " + values.GetTuple1(i));
		}
	}
}