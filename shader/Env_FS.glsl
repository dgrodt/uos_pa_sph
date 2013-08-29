#version 330

#include "shader/ShaderGlobals.glslh"

in vec2 fs_in_tc;
in vec3 fs_in_normal;
in vec4 fs_in_ViewPos;
in vec3 fs_in_pos;
uniform sampler2D ceilingTex;
uniform sampler2D floorTex;
uniform sampler2D wallTex;

out vec4 fs_out_color;
void main()
{
	if(fs_in_normal.x == 0 && fs_in_normal.z == 0 && fs_in_normal.y > 0) {
    	fs_out_color = texture(ceilingTex, fs_in_tc);
    } else if(fs_in_normal.x == 0 && fs_in_normal.z == 0 && fs_in_normal.y < 0) {
    	fs_out_color = texture(floorTex, fs_in_tc);
    } else {
    	fs_out_color = texture(wallTex, fs_in_tc);
    }
}