#version 330

#include "shader/ShaderGlobals.glslh"

in vec2 fs_in_tc;
in vec3 fs_in_normal;
in vec4 fs_in_ViewPos;
in vec3 fs_in_pos;
uniform sampler2D ceilingTex;
uniform sampler2D floorTex;
uniform sampler2D wallTex;

const vec4 fog_colour = vec4 (0.0, 0.0, 0.0, 1);
const float min_fog_radius = 3.0;
const float max_fog_radius = 10.0;

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
    	col = texture(ceilingTex, fs_in_tc);
    } else if(fs_in_normal.x == 0 && fs_in_normal.z == 0 && fs_in_normal.y < 0) {
    	col = texture(floorTex, fs_in_tc);
    } else {
    	col = texture(wallTex, fs_in_tc);
    }
    // blend the fog colour with the lighting colour, based on the fog factor
	fs_out_color = mix (col, fog_colour, fog_fac);
	fs_out_color.a = 1.0f;
}