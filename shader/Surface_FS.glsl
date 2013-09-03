#version 330

#include "shader/ShaderGlobals.glslh"

in vec2 fs_in_tc;
in vec3 fs_in_normal;
in vec4 fs_in_ViewPos;
in vec3 fs_in_world;
in vec3 refractedVector;

layout(location = 0) out vec4 fs_out_color;
layout(location = 1) out float fs_out_depth;
layout(location = 2) out vec3 fs_out_world;
layout(location = 3) out vec3 fs_out_normal;
layout(location = 4) out float fs_out_specular;
layout(location = 5) out float fs_out_diffuse;
layout(location = 6) out vec3 fs_out_freshnel;

void main()
{
	vec3 normal = fs_in_normal.xyz;
    vec4 worldView = vec4(fs_in_world,1);
    
    fs_out_color = g_color;
    fs_out_depth = 1-normalize(fs_in_ViewPos).z;
    fs_out_world = fs_in_world;
    fs_out_normal = normalize(fs_in_normal);
    vec2 diff_spec = getLight(worldView, g_lightPos,  normal);
    fs_out_specular =  diff_spec.y;
    fs_out_diffuse  =  diff_spec.x;
}