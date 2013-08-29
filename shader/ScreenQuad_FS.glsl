#version 330
#include "shader/ShaderGlobals.glslh"

uniform sampler2D color;
uniform sampler2D depth;
uniform sampler2D world;
uniform sampler2D normal;
uniform sampler2D specular;
uniform sampler2D diffuse;
uniform sampler2D freshel;
uniform sampler2D thickness;
in vec2 fs_in_tc;
in vec4 fs_in_ViewPos;
out vec4 fs_out_color;

uniform float offset[5] = float[]( 0.0, 1.0, 2.0, 3.0, 4.0 );
uniform float weight[25] = 
float[]
(0.01, 0.02, 0.04, 0.02, 0.01,
 0.02, 0.04, 0.08, 0.04, 0.02,
 0.04, 0.08, 0.16, 0.08, 0.04,
 0.02, 0.04, 0.08, 0.04, 0.02,
 0.01, 0.02, 0.04, 0.02, 0.01);

void main()
{
	float blurSize = 2f/768f;
	vec2 fragCoords = vec2(fs_in_tc.x, 1-fs_in_tc.y);

	vec4 sum = vec4(0.0);   
    for(int i = -2; i <= 2; i++){
    	for(int j = - 2; j <= 2; j++){
    		sum += texture(thickness, vec2(fragCoords.x + i*blurSize, fragCoords.y + j*blurSize)) * weight[(i+2) + (j+2) * 5];
    	}
    }

    fs_out_color = (texture(color, fragCoords) * texture(diffuse, fragCoords) + vec4(1) * texture(specular, fragCoords));
    //fs_out_color = sum;
    //fs_out_color += vec4(1) * texture(specular, fragCoords).x;
    //fs_out_color = (cofs.x * vec4(0.1) + cofs.y * texture(color, fragCoords) + vec4(1) * g_ambient) ;//* sum;


}