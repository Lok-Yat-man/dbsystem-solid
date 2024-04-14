import {ScatterplotLayer} from '@deck.gl/layers';
import customFragmentShader from './rectangle-layer-fragment.js';

export class RectangleLayer extends ScatterplotLayer {

  draw({uniforms}) {
    super.draw({
      uniforms:
        {
        ...uniforms,
        cornerRadius: this.props.cornerRadius
        }
    })
  }

  getShaders() {
    // use object.assign to make sure we don't overwrite existing fields like `vs`, `modules`...
    return Object.assign({}, super.getShaders(), {
      fs: customFragmentShader
    });
  }
}

RectangleLayer.defaultProps = {
  // cornerRadius: the amount of rounding at the rectangle corners
  // 0 - rectangle. 1 - circle.
  cornerRadius: 0
}