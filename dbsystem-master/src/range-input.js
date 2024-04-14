/* global requestAnimationFrame, cancelAnimationFrame */
import React, {useEffect, useState} from 'react';
import {styled, withStyles} from '@material-ui/core/styles';
import Slider from '@material-ui/core/Slider';
import Button from '@material-ui/core/IconButton';
import PlayIcon from '@material-ui/icons/PlayArrow';
import PauseIcon from '@material-ui/icons/Pause';
import { pink } from '@mui/material/colors';
import Box from '@mui/material/Box';


const PositionContainer = styled('div')({
  position: 'absolute',
  zIndex: 1,
  bottom: '120px',
  width: '100%',
  display: 'flex',
  justifyContent: 'center',
  alignItems: 'center'
});

const SliderInput = withStyles({
  root: {
    marginLeft: 12,
    width: '60%',
    color: '#FF0000'
  },
  valueLabel: {
    '& span': {
      background: 'none',
      color: '#FFFFFF'
    }
  },
  
  
})(Slider);

export default function RangeInput({min, max, value, animationSpeed, onChange, formatLabel}) {
  const [isPlaying, setIsPlaying] = useState(false);
  const [animation] = useState({});

  // prettier-ignore
  useEffect(() => {
    return () => animation.id && cancelAnimationFrame(animation.id);
  }, [animation]);

  if (isPlaying && !animation.id) {
    const span = 0
    let nextValueMin = value + animationSpeed;
    if (nextValueMin + span >= max) {
      nextValueMin = min;
    }
    animation.id = requestAnimationFrame(() => {
      animation.id = 0;
      onChange(nextValueMin + span);
    });
  }

  const isButtonEnabled = true

  return (

    <PositionContainer>
      <Button color="primary" disabled={!isButtonEnabled} onClick={() => setIsPlaying(!isPlaying)}>
        {isPlaying ? <PauseIcon title="Stop" color="secondary" /> : <PlayIcon title="Animate" color="secondary" />}
      </Button>
      <SliderInput
        min={min}
        max={max}
        step={0.25/4}
        value={value}
        onChange={(event, newValue) => onChange(newValue)}
        valueLabelDisplay="on"
        valueLabelFormat={formatLabel}
      />
    </PositionContainer>
  );
}

