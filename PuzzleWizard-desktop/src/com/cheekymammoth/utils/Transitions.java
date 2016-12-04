package com.cheekymammoth.utils;

import com.badlogic.gdx.math.Interpolation;

//Easing functions from http://dojotoolkit.org and http://www.robertpenner.com/easing
public abstract class Transitions extends Interpolation {

	@Override
	public float apply(float a) {
		return 0;
	}
	
	static public final Interpolation linear = new Interpolation() {
        public float apply (float a) {
        	return a;
        }
	};
	
	static public final Interpolation easeInLinear = new Interpolation() {
        public float apply (float a) {
        	return a * a;
        }
	};
	
	static public final Interpolation easeIn = new Interpolation() {
        public float apply (float a) {
        	return a * a * a;
        }
	};
	
	static public final Interpolation easeOut = new Interpolation() {
        public float apply (float a) {
        	float invRatio = a - 1.0f;
        	return invRatio * invRatio * invRatio + 1.0f;
        }
	};
	
	static public final Interpolation easeInOut = new Interpolation() {
        public float apply (float a) {
        	return ((a < 0.5f) ? 0.5f * easeIn.apply(a * 2.0f) : 0.5f * easeOut.apply((a - 0.5f) * 2.0f) + 0.5f);
        }
	};
	
	static public final Interpolation easeOutIn = new Interpolation() {
        public float apply (float a) {
        	return ((a < 0.5f) ? 0.5f * easeOut.apply(a * 2.0f) : 0.5f * easeIn.apply((a - 0.5f) * 2.0f) + 0.5f);
        }
	};
	
	static public final Interpolation easeInBack = new Interpolation() {
        public float apply (float a) {
        	float s = 1.70158f;
        	return (float)Math.pow((double)a, 2.0) * ((s + 1.0f) * a - s);
        }
	};
	
	static public final Interpolation easeOutBack = new Interpolation() {
        public float apply (float a) {
        	float invRatio = a - 1.0f, s = 1.70158f;
        	return (float)Math.pow((double)invRatio, 2.0) * ((s + 1.0f) * invRatio + s) + 1.0f;
        }
	};
	
	static public final Interpolation easeInOutBack = new Interpolation() {
        public float apply (float a) {
        	return ((a < 0.5f) ?
        			0.5f * easeInBack.apply(a * 2.0f) :
        				0.5f * easeOutBack.apply((a - 0.5f) * 2.0f) + 0.5f);
        }
	};
	
	static public final Interpolation easeOutInBack = new Interpolation() {
        public float apply (float a) {
        	return ((a < 0.5f) ?
        			0.5f * easeOutBack.apply(a * 2.0f) :
        				0.5f * easeInBack.apply((a - 0.5f) * 2.0f) + 0.5f);
        }
	};
	
	static public final Interpolation easeInElastic = new Interpolation() {
        public float apply (float a) {
        	if (a == 0.0f || a == 1.0f)
        		return a;
            else {
                float p = 0.3f;
                float s = p / 4.0f;
                float invRatio = a - 1.0f;
                return -1.0f * (float)Math.pow(2.0f, 10.0 * invRatio) * (float)Math.sin((invRatio - s) * Math.PI / p);
            }
        }
	};
	
	static public final Interpolation easeOutElastic = new Interpolation() {
        public float apply (float a) {
        	if (a == 0.0f || a == 1.0f)
        		return a;
            else {
                float p = 0.3f;
                float s = p / 4.0f;
                return -1.0f * (float)Math.pow(2.0f, -10.0 * a) * (float)Math.sin((a - s) * Math.PI / p) + 1.0f;
            }
        }
	};
	
	static public final Interpolation easeInOutElastic = new Interpolation() {
        public float apply (float a) {
        	return (a < 0.5f) ? 0.5f * easeInElastic.apply(a * 2.0f) : 0.5f * easeOutElastic.apply((a - 0.5f) * 2.0f) + 0.5f;
        }
	};
	
	static public final Interpolation easeOutInElastic = new Interpolation() {
        public float apply (float a) {
        	return (a < 0.5f) ? 0.5f * easeOutElastic.apply(a * 2.0f) : 0.5f * easeInElastic.apply((a - 0.5f) * 2.0f) + 0.5f;
        }
	};
	
	static public final Interpolation easeInBounce = new Interpolation() {
        public float apply (float a) {
        	return 1.0f - easeOutBounce.apply(1.0f - a);
        }
	};
	
	static public final Interpolation easeOutBounce = new Interpolation() {
        public float apply (float a) {
        	float s = 7.5625f;
            float p = 2.75f;
            float l;

            if (a < (1.0f / p))
                l = s * (float)Math.pow(a, 2.0);
            else {
                if (a < (2.0f / p)) {
                    a -= 1.5f / p;
                    l = s * (float)Math.pow(a, 2.0f) + 0.75f;
                } else {
                    a -= 2.625f / p;
                    l = s * (float)Math.pow(a, 2.0f) + 0.984375f;
                }
            }

            return l;
        }
	};
	
	static public final Interpolation easeInOutBounce = new Interpolation() {
        public float apply (float a) {
        	return (a < 0.5f) ? 0.5f * easeInBounce.apply(a * 2.0f) : 0.5f * easeOutBounce.apply((a - 0.5f) * 2.0f) + 0.5f;
        }
	};
	
	static public final Interpolation easeOutInBounce = new Interpolation() {
        public float apply (float a) {
        	return (a < 0.5f) ? 0.5f * easeOutBounce.apply(a * 2.0f) : 0.5f * easeInBounce.apply((a - 0.5f) * 2.0f) + 0.5f;
        }
	};

}
