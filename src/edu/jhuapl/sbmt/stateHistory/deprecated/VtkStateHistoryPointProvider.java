package edu.jhuapl.sbmt.stateHistory.deprecated;
//package edu.jhuapl.sbmt.stateHistory.rendering.vtk;
//
//import vtk.vtkPoints;
//
//import edu.jhuapl.saavtk.vtk.VtkResource;
//
///**
// * Object that provides access to lidar points backed via VTK data structures.
// *
// * @author lopeznr1
// */
//public class VtkStateHistoryPointProvider implements VtkResource
//{
//	// VTK vars
//	private final vtkPoints vPointsSource;
//	private final vtkPoints vPointsTarget;
//
//	/**
//	 * Standard Constructor
//	 * <P>
//	 * Note a clone of the provided VTK objects will be created.
//	 */
//	public VtkStateHistoryPointProvider(vtkPoints aPointsSrc, vtkPoints aPointsTgt)
//	{
//		vPointsSource = new vtkPoints();
//		vPointsSource.DeepCopy(aPointsSrc);
//		vPointsTarget = new vtkPoints();
//		vPointsTarget.DeepCopy(aPointsTgt);
//	}
//
//	/**
//	 * Returns the number of points in this provider.
//	 */
//	public int getNumberOfPoints()
//	{
//		return vPointsTarget.GetNumberOfPoints();
//	}
//
//	/**
//	 * Returns the source position for the lidar point at the specified index.
//	 */
//	public double[] getSourcePosition(int aIdx)
//	{
//		return vPointsSource.GetPoint(aIdx);
//	}
//
//	/**
//	 * Returns the target position for the lidar point at the specified index.
//	 */
//	public double[] getTargetPosition(int aIdx)
//	{
//		return vPointsTarget.GetPoint(aIdx);
//	}
//
//	@Override
//	public void vtkDispose()
//	{
//		vPointsSource.Delete();
//		vPointsTarget.Delete();
//	}
//
//	@Override
//	public void vtkUpdateState()
//	{
//		; // Nothing to do
//	}
//
//}
