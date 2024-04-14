/// rounded-rectangle-layer-fragment.js
// This is copied and adapted from scatterplot-layer-fragment.glsl.js
// Modifications are annotated
export default `\
#define SHADER_NAME rectangle-layer-fragment-shader

precision highp float;

uniform float cornerRadius;

varying vec4 vFillColor;
varying vec2 unitPosition;

void main(void) {

  float distToCenter = length(unitPosition);

  /* Calculate the cutoff radius for the rounded corners */
  gl_FragColor = vFillColor;


  gl_FragColor = picking_filterHighlightColor(gl_FragColor);

  gl_FragColor = picking_filterPickingColor(gl_FragColor);
}
`;