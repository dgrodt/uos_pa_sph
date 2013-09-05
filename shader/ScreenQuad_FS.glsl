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
uniform sampler2D background;

uniform int colorMode;

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
	vec2 fragCoords = vec2(fs_in_tc.x, 1-fs_in_tc.y);

	float blurSize = 2f/768f;
	vec4 blur_thickness = vec4(0.0);   
    for(int i = -2; i <= 2; i++){
    	for(int j = - 2; j <= 2; j++){
    		blur_thickness += texture(thickness, vec2(fragCoords.x + i*blurSize, fragCoords.y + j*blurSize)) * weight[(i+2) + (j+2) * 5];
    	}
    }
    blurSize = 2f/768f;
    vec4 blur_spec = vec4(0.0); 
    for(int i = -2; i <= 2; i++){
    	for(int j = - 2; j <= 2; j++){
    		blur_spec += texture(specular, vec2(fragCoords.x + i*blurSize, fragCoords.y + j*blurSize)) * weight[(i+2) + (j+2) * 5];
    	}
    }
    
    //-----------------------------------------------------//
    //				 	Freshnel Try					   //
    //-----------------------------------------------------//
/*	vec3 I = normalize(texture(world,fragCoords) - g_eye).xyz;
	vec3 N = normalize(texture(normal,fragCoords)).xyz;

	vec3 refractedVector = refract(I,N,0.5f);
	float reflectionCoeff = max(0, min(1, 1.5f +  dot(I,N)));
	refractedVector *= reflectionCoeff;
	
	vec3 s = (texture(world,fragCoords) - g_eye).xyz + refractedVector;
	s = (g_view*g_projection*vec4(s,1)).xyz ;
	vec2 coords = (0.01 / s.z) * s.xy;
    vec2 freshnelCoords = fragCoords + refractedVector.xy* vec2(1.0/1024.0,1.0/768.0);
    if(texture(depth,fragCoords).r == 0){
   		fs_out_color = texture(background, fragCoords);
    } else {
    	fs_out_color = texture(background, freshnelCoords);
    }
*/
	fs_out_color = texture(background, fragCoords);	    
   
	if(colorMode == 0) {
    	fs_out_color += ((texture(color,fragCoords) + (vec4(0.8) * blur_spec.r))*blur_thickness)+vec4(0.2)*texture(diffuse,fragCoords).r;
    } else if(colorMode == 1) {
    	fs_out_color += texture(color,fragCoords);
    } else if(colorMode == 2) {
    	fs_out_color += texture(thickness,fragCoords);
    } else if(colorMode == 3) {
    	fs_out_color += vec4(1)*texture(specular,fragCoords).r;
    } else if(colorMode == 4) {
    	fs_out_color += vec4(1)*texture(diffuse,fragCoords).r;
    } else if(colorMode == 5) {
    	fs_out_color += texture(normal,fragCoords);
    } else if(colorMode == 6) {
    	fs_out_color += texture(depth,fragCoords);
    } else if(colorMode == 7) {
    	fs_out_color += texture(world,fragCoords);
    } else if(colorMode == 8) {
    	fs_out_color += ((-texture(color,fragCoords) + (vec4(0.9) * blur_spec.r))*0.9*blur_thickness)+vec4(0.5)*texture(diffuse,fragCoords).r;
    }
}