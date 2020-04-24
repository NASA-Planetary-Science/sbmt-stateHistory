package edu.jhuapl.sbmt.stateHistory.rendering.vtk;

import java.util.Iterator;

import vtk.vtkCellArray;
import vtk.vtkDataObject;
import vtk.vtkDoubleArray;
import vtk.vtkGeometryFilter;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkUnsignedCharArray;
import vtk.vtkVertex;

import edu.jhuapl.sbmt.lidar.LidarPoint;
import edu.jhuapl.sbmt.lidar.feature.FeatureAttr;
import edu.jhuapl.sbmt.lidar.feature.FeatureAttrBuilder;
import edu.jhuapl.sbmt.lidar.feature.VtkFeatureAttr;

/**
 * Collection of VTK based utility methods.
 *
 * @author lopeznr1
 */
public class VtkStateHistoryUtil
{
	// Constants
	// An empty PolyData used for resetting
	private static final vtkPolyData EmptyPolyData = formEmptyPolyData();

	/**
	 * Utility method used to reset a vtkPolyData
	 */
	public static void clearPolyData(vtkPolyData aPolyData)
	{
		aPolyData.DeepCopy(EmptyPolyData);
	}

	/**
	 * Utility method that will create a vtkGeometryFilter for the specified
	 * vtkDataObject.
	 */
	public static vtkGeometryFilter formGeometryFilter(vtkDataObject aVtkDataObject, int aNumPts)
	{
		vtkGeometryFilter retVtkGeometryFilter = new vtkGeometryFilter();
		retVtkGeometryFilter.SetInputData(aVtkDataObject);
		retVtkGeometryFilter.PointClippingOn();
		retVtkGeometryFilter.CellClippingOff();
		retVtkGeometryFilter.ExtentClippingOff();
		retVtkGeometryFilter.MergingOff();
		retVtkGeometryFilter.SetPointMinimum(0);
		retVtkGeometryFilter.SetPointMaximum(aNumPts);

		return retVtkGeometryFilter;
	}

	/**
	 * Utility method that will create a {@link VtkStateHistoryStruct} for the specified
	 * parameters.
	 * <P>
	 * The returned {@link VtkStateHistoryStruct} life cycle must be managed by the
	 * caller of this method.
	 *
	 * @param aPointIter The LidarPoints associated with the lidar data object.
	 */
	public static VtkStateHistoryStruct formVtkLidarStruct(Iterator<LidarPoint> aPointIter)
	{
		FeatureAttrBuilder intensityFAB = new FeatureAttrBuilder();
		vtkDoubleArray vRadiusDA = new vtkDoubleArray();
		vtkDoubleArray vRangeDA = new vtkDoubleArray();
		vtkDoubleArray vTimeDA = new vtkDoubleArray();
		vtkPoints vSrcP = new vtkPoints();
		vtkPoints vTgtP = new vtkPoints();
		vtkCellArray vSrcCA = new vtkCellArray();
		vtkCellArray vTgtCA = new vtkCellArray();

		// Update the VTK state to reflect the provided LidarPoints
		while (aPointIter.hasNext())
		{
			LidarPoint pt = aPointIter.next();

			int tgtId = vTgtP.InsertNextPoint(pt.getTargetPosition().toArray());
			vtkVertex tgtV = new vtkVertex();
			tgtV.GetPointIds().SetId(0, tgtId);
			vTgtCA.InsertNextCell(tgtV);

			// Keep track of features of interest
			double radius = pt.getTargetPosition().getNorm();
			vRadiusDA.InsertNextValue(radius);

			double range = pt.getSourcePosition().subtract(pt.getTargetPosition()).getNorm();
			vRangeDA.InsertNextValue(range);

			double irec = pt.getIntensityReceived();
			intensityFAB.addValue(irec);

			int srcId = vSrcP.InsertNextPoint(pt.getSourcePosition().toArray());
			vtkVertex srcV = new vtkVertex();
			srcV.GetPointIds().SetId(0, srcId);
			vSrcCA.InsertNextCell(srcV);
			vTimeDA.InsertNextValue(pt.getTime());
		}

		// Instantiate the VtkLidarStruct
		FeatureAttr timeFA = new VtkFeatureAttr(vTimeDA);
		FeatureAttr radiusFA = new VtkFeatureAttr(vRadiusDA);
		FeatureAttr rangeFA = new VtkFeatureAttr(vRangeDA);
		FeatureAttr intensityFA = intensityFAB.build();

		VtkStateHistoryStruct retVLS = new VtkStateHistoryStruct(timeFA, radiusFA, rangeFA, intensityFA, vSrcP, vSrcCA, vTgtP, vTgtCA);
		return retVLS;
	}

	/**
	 * Utility helper method that forms a vtkPolyData that is useful for
	 * resetting other vtkPolyData.
	 */
	private static vtkPolyData formEmptyPolyData()
	{
		vtkPolyData retPD = new vtkPolyData();
		vtkPoints vTmpP = new vtkPoints();
		vtkCellArray vTmpCA = new vtkCellArray();

		// Initialize an empty vtkPolyData for resetting
		retPD.SetPoints(vTmpP);
		retPD.SetVerts(vTmpCA);

		vtkUnsignedCharArray colorUCA = new vtkUnsignedCharArray();
		colorUCA.SetNumberOfComponents(4);
		retPD.GetCellData().SetScalars(colorUCA);

		return retPD;
	}

}
