using UnityEngine;
using System.Collections;

public class NativeRectManager : UnityEngine.Object {

	private const float NOTHING_VALUE = -1.0f;

	private float x;
	public float X {
		get { return x;}
	}
	private float y;
	public float Y {
		get { return y;}
	}
	private float width;
	public float Width {
		get { return width;}
	}
	private float height;
	public float Height {
		get { return height;}
	}

	// constructor
	public NativeRectManager() {
		initRectValues ();
	}

	public static Rect GetScreenRectFromRectTransform(RectTransform rectTransform)
	{
		Vector3[] corners = new Vector3[4];

		rectTransform.GetWorldCorners(corners);

		float xMin = float.PositiveInfinity;
		float xMax = float.NegativeInfinity;
		float yMin = float.PositiveInfinity;
		float yMax = float.NegativeInfinity;

		for (int i = 0; i < 4; i++)
		{
			// For Canvas mode Screen Space - Overlay there is no Camera; best solution I've found
			// is to use RectTransformUtility.WorldToScreenPoint) with a null camera.
			Vector3 screenCoord = RectTransformUtility.WorldToScreenPoint(null, corners[i]);

			if (screenCoord.x < xMin)
				xMin = screenCoord.x;
			if (screenCoord.x > xMax)
				xMax = screenCoord.x;
			if (screenCoord.y < yMin)
				yMin = screenCoord.y;
			if (screenCoord.y > yMax)
				yMax = screenCoord.y;
		}
		Rect result = new Rect(xMin, Screen.height - yMax, xMax - xMin, yMax - yMin);
		return result;
	}

	private void initRectValues(){
		x =	NOTHING_VALUE;
		y = NOTHING_VALUE;
		width = NOTHING_VALUE;
		height = NOTHING_VALUE;
	}

	/*
	 * <summary>
	 * update rectTransform
	 * </summary>
	 * <args>
	 * 	RectTransform rectTrans
	 * </args>
	 * <return>
	 * 	bool isChanged
	 * </return>
	 */ 
	public bool updateRect(RectTransform rectTrans){

		bool isChanged = false;

		Rect rectScreen = GetScreenRectFromRectTransform (rectTrans);

		float nextX = rectScreen.x / Screen.width;
		float nextY =  rectScreen.y / Screen.height;
		float nextWidth = rectScreen.width / Screen.width;
		float nextHeight = rectScreen.height / Screen.height;

		if (nextX != x) {
			isChanged = true;
			x = nextX;
		}

		if (nextY != y) {
			isChanged = true;
			y = nextY;
		}

		if (nextWidth != width) {
			isChanged = true;
			width = nextWidth;
		}

		if (nextHeight != height){
			isChanged = true;
			height = nextHeight;
		}

		return isChanged;
	}

}
