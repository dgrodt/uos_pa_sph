#version 330
#extension GL_ARB_separate_shader_objects : enable
#include "shader/ShaderGlobals.glslh"

in vec2 fs_in_tc;
in vec3 fs_in_normal;
in vec4 fs_in_ViewPos;
in vec3 fs_in_pos;
uniform sampler2D floorTex;
uniform sampler2D floorBumpTex;


const vec4 fog_colour = vec4 (0.0, 0.0, 0.0, 1);
const float min_fog_radius = 5.0;
const float max_fog_radius = 15.0;

out vec4 fs_out_color;

void main()
{
	vec4 col;
	
	// work out distance from camera to point
	float dist = length (g_eye-fs_in_ViewPos);
	// get a fog factor (thickness of fog) based on the distance
	float fog_fac = (dist - min_fog_radius) / (max_fog_radius - min_fog_radius);
	// constrain the fog factor between 0 and 1
	fog_fac = clamp (fog_fac, 0.0, 1.0);
	
	if(fs_in_normal.x == 0 && fs_in_normal.z == 0 && fs_in_normal.y > 0) {
    	//col = texture(ceilingTex, fs_in_tc);
    	col = vec4(0, 0, 0, 1);
    } else if(fs_in_normal.x == 0 && fs_in_normal.z == 0 && fs_in_normal.y < 0) {
    	vec4 normalTex = texture(floorBumpTex, fs_in_tc);
    	//vec2 light = getLight(vec4(fs_in_ViewPos), g_lightPos, normalize(normalTex.xyz));
    	vec2 light = getSpecDiffuseCoe(normalize(normalTex.xyz), g_eye.xyz, fs_in_pos, g_lightPos);
    	col= texture(floorTex, fs_in_tc);
    	col = vec4(0.15)*light.x + light.y*col*0.7 ;
    } else {
    	//col = texture(wallTex, fs_in_tc);
    	col = vec4(0, 0, 0, 1);
    }
    // blend the fog colour with the lighting colour, based on the fog factor
	fs_out_color = mix(col, fog_colour, fog_fac);
	//fs_out_color = col;
	fs_out_color.a = 1.0f;
}