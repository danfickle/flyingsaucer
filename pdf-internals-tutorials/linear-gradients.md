PDF Tutorial
============
This tutorial assumes a basic knowledge of PDF syntax and objects such as dictionaries.

Linear gradients without transparency
-------------------------------------
Linear gradients in PDF are implemented using a pattern dictionary which defines a shading, which itself
uses a function to interpolate values.

We start with the `/Pattern` entry in the page `/Resources` dictionary:
````
% In the page's resources dictionary.
/Pattern             % Start the listing of patterns.
<<
  /LGradient0        % Use a unique generated resource name.
  <<
    /Type /Pattern   % Optional.
    /PatternType 2   % Specifies an axial shading (linear gradient).
    /Shading 9 0 R   % A reference to our shading dictionary.
  >>
  /LGradient1        % A second gradient is specified here.
  <<
    /Type /Pattern
    /PatternType 2
    /Shading 12 0 R
  >>
>>
````
Next we jump to object 9, the shading dictionary:
````
9 0 obj
<<
  /ShadingType 2           % Axial shading type.
  /Extend [true true]      % Whether to extend the shading past the start and ending points.
  /ColorSpace /DeviceRGB   % Color space, we're using RGB. Note this does not include an alpha component.
  /Coords [499.725 682.125 69.75 682.125]  % The starting and ending x, y pairs. This lets you rotate a linear gradient.
  /Function 10 0 R         % Reference to our shading function.
>>
endobj
````
Then we move to object 10, the shading function. We're going to use a type 3 stitching function in case there are more than two
color stops in our linear gradient. This example shows a stitching function for three color stops.
If using only two color stops you can use a type 2 function directly.
````
10 0 obj
<<
  /FunctionType 3          % A function that stitches together other functions.
  /Domain [0 1]            % Domain (min and max).
  /Bounds [0.5 ]           % The split points, don't include first(0) and last(1).
  /Encode [0 1 0 1 ]       % A pair of encoding for each color stop. Typically 0 1, 0 1, etc.
  /Functions               % Our array of functions. There should be one less than the number of color stops.
  [
   17 0 R                  % Start to 2nd color stop.
   18 0 R                  % 2nd color stop to end.
  ]
>>
endobj
````
Next, we show an actual terminal function:
````
17 0 obj
<<
  /FunctionType 2  % Interpolation function.
  /Domain [0 1]
  /N 1             % The exponent, 1 for linear interpolation.
  /C0 [1 0 0]      % First color, in this case red.
  /C1 [0 0.502 0]  % Second color, in this case partially green.
>>
endobj
````
Finally, we show how to invoke a shading pattern in the graphics stream for a page:
````
q                             % Save the state of the stack.
/Pattern cs                   % Switch to pattern color space.
/LGradient0 scn               % Choose our pattern for non-stroking operations (such as paint).
69.75 682.125 429.975 75 re   % Define a rectangle path.
f                             % Fill the rectangle.
Q                             % Restore the graphics stack.
````

Linear gradients with transparency
----------------------------------
Linear gradients with transparency involve a lot more complexity. We begin by defining a `ExtGState` (extended graphics state)
which will hold our soft mask (transparency mask) to use with our linear gradient which **can be defined as above**.
ExtGStates are listed in the page's resources dictionary:
````
% In page resources dictionary.
/ExtGState
<<
  /LGGS5 33 0 R   % A generated resource name pointing to a ExtGState dictionary.
  /LGGS12 72 0 R  % A second item.
>>
````
Our actual `ExtGState` looks like:
````
33 0 obj
<<
  /AIS false         % Specifies that this will point to a opacity soft mask rather than a shape mask. 
  /SMask 32 0 R      % Pointer to the transparency mask.
  /Type /ExtGState   % Dictionary type.
>>
endobj
````
Next, let's look at the `SMask` dictionary:
````
32 0 obj
<<
/G 31 0 R            % Link to a transparency group XObject
/S /Luminosity       % Alpha or Luminosity will give different effects.
/Type /Mask          % Dictionary type.
>>
endobj
````
And then the 'transparency group XObject':
````
31 0 obj
<<
  /BBox [0 0 435 346.125]      % Bounding box, in units specific to this XObject.
  /FormType 1
  /Group
  <<
    /CS /DeviceGray            % DeviceGray is used to specify alpha values.
    /S /Transparency           % A transparency group.
    /Type /Group
  >>
  /Resources                   % Resources specific to this XObject.
  <<
    /Pattern
    <<
      /Pat31                   % A generated resource name.
      <<
        /Type /Pattern
        /PatternType 2         % Axial shading.
        /Shading
        <<
          /ColorSpace /DeviceGray
          /Extend [true true]
          /ShadingType 2
          /Function 34 0 R     % A link to our function that will specify the alpha values.
          /Coords [23.831 115.889 357.019 -36.511]   % Coordinates for starting and ending point, used for rotation.
        >>
      >>
    >>
  >>
  /Subtype /Form
  /Type /XObject
  /Length 56   % Won't be correct with added comments!
>>
stream
q  % Save the graphic state.
/Pattern cs  % Pattern color space.
/Pat31 scn   % Choose pattern
58.5 288.375 376.5 57.75 re  % A rectangle path
f            % Fill the path.
Q            % Pop the graphics save stack.
endstream
endobj
````
The stitching function is very similar to the one above. This stitching function contains functions for 5 color stops (including
start and end).
````
34 0 obj
<<
  /FunctionType 3
  /Domain [0 1]
  /Bounds [0.33 0.66 0.99 ]
  /Encode [0 1 0 1 0 1 0 1 ]
  /Functions [
    35 0 R
    36 0 R
    37 0 R
    38 0 R
  ]
>>
endobj
````
And here is one of the final functions:
````
36 0 obj
<<
  /FunctionType 2
  /Domain [0 1]
  /N 1
  /C0 [0.2]   % First alpha value.
  /C1 [0.4]   % Second alpha value.
>>
````
When we have defined our patterns with all their normal RGB functions(see first part of tutorial) and DeviceGray alpha functions
we can use it simply in a graphics stream:
````
q
/LGGS12 gs                      % Select our transparency defining ExtGState.
/Pattern cs
/LGradient12 scn                % Select our RGB defining pattern.
58.5 1058.25 376.5 57.75 re     % A rectangle.
f                               % Fill it.
Q
````

That's it for this tutorial. If you have any issues, please open an issue with this project (danfickle/neoflyingsaucer).
Please consider this tutorial licensed under the Creative Commons Attribution license. Thankyou for reading.

