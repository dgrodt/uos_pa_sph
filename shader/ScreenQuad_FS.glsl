#version 330
#include "shader/ShaderGlobals.glslh"

uniform sampler2D color;
uniform sampler2D depth;
uniform sampler2D normal;
uniform sampler2D world;

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
	float blurSize = 1f/768f;
	vec2 fragCoords = vec2(fs_in_tc.x, 1-fs_in_tc.y);
	fs_out_color = texture2D(color, fragCoords);
	/*
	vec3 normal = normalize(cross(texture2D(color, vec2(fragCoords.x - 5, fragCoords.y)).xyz - texture2D(color, fragCoords).xyz, texture2D(color, vec2(fragCoords.x, fragCoords.y - 5)).xyz - texture2D(color, fragCoords).xyz));
    */
    vec3 normalvec = texture2D(normal,fragCoords).xyz;
    
    vec3 worldView = texture2D(world, fragCoords).xyz;
    
    vec3 lightPos = (vec4(g_lightPos, 1)).xyz;
    
    vec3 eye = (g_view * vec4(g_eye.xyz,1)).xyz;
    
    vec2 cofs = getSpecDiffuseCoe(normalvec, eye, worldView, lightPos);

	vec4 sum = vec4(0.0);
	   
    for(int i = -2; i <= 2; i++){
    	for(int j = - 2; j <= 2; j++){
    		sum += texture2D(color, vec2(fragCoords.x + i*blurSize, fragCoords.y + j*blurSize)) * weight[(i+2) + (j+2) * 5];
    	}
    }
    /*
    // blur in y (vertical)
    // take nine samples, with the distance blurSize between them
    sum += texture2D(color, vec2(fragCoords.x, fragCoords.y - 4.0*blurSize)) * 0.05;
    sum += texture2D(color, vec2(fragCoords.x, fragCoords.y - 3.0*blurSize)) * 0.09;
    sum += texture2D(color, vec2(fragCoords.x, fragCoords.y - 2.0*blurSize)) * 0.12;
    sum += texture2D(color, vec2(fragCoords.x, fragCoords.y - blurSize)) * 0.15;
    sum += texture2D(color, vec2(fragCoords.x, fragCoords.y)) * 0.16;
    sum += texture2D(color, vec2(fragCoords.x, fragCoords.y + blurSize)) * 0.15;
    sum += texture2D(color, vec2(fragCoords.x, fragCoords.y + 2.0*blurSize)) * 0.12;
    sum += texture2D(color, vec2(fragCoords.x, fragCoords.y + 3.0*blurSize)) * 0.09;
    sum += texture2D(color, vec2(fragCoords.x, fragCoords.y + 4.0*blurSize)) * 0.05;
    
    // blur in x (horizontal)
    // take nine samples, with the distance blurSize between them
    sum += texture2D(color, vec2(fragCoords.x - 4.0*blurSize, fragCoords.y)) * 0.05;
    sum += texture2D(color, vec2(fragCoords.x - 3.0*blurSize, fragCoords.y)) * 0.09;
    sum += texture2D(color, vec2(fragCoords.x - 2.0*blurSize, fragCoords.y)) * 0.12;
    sum += texture2D(color, vec2(fragCoords.x - blurSize, fragCoords.y)) * 0.15;
    sum += texture2D(color, vec2(fragCoords.x, fragCoords.y)) * 0.16;
    sum += texture2D(color, vec2(fragCoords.x + blurSize, fragCoords.y)) * 0.15;
    sum += texture2D(color, vec2(fragCoords.x + 2.0*blurSize, fragCoords.y)) * 0.12;
    sum += texture2D(color, vec2(fragCoords.x + 3.0*blurSize, fragCoords.y)) * 0.09;
    sum += texture2D(color, vec2(fragCoords.x + 4.0*blurSize, fragCoords.y)) * 0.05;
    */
    
    fs_out_color =  sum;
    fs_out_color.w = 1-texture2D(depth, fragCoords).x;
    
    //fs_out_color = (cofs.x * vec4(0.1) + cofs.y * texture2D(color, fragCoords) + vec4(1) * g_ambient) ;//* sum;
    //fs_out_color = texture2D(color, fragCoords);

}