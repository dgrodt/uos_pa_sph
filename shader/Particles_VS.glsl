#version 330

#include "shader/ShaderGlobals.glslh"

uniform mat4x4 invCamera;

in vec3 vs_in_pos;
in vec3 vs_in_normal;
in vec2 vs_in_tc;
in vec4 vs_in_instance;
in vec4 vs_in_normal_new;

out vec2 fs_in_tc;
out vec3 fs_in_normal;
out vec4 gl_Position;
out vec4 fs_in_ViewPos;
out float fs_in_depth;
out vec3 fs_in_worldPos;
out vec4 fs_in_normal_new;


void main()
{

    fs_in_normal = vs_in_normal; //g_model
    fs_in_normal_new = vs_in_normal_new;
    fs_in_tc = vs_in_tc;
    
    vec4 vPos = invCamera * vec4(vs_in_pos, 0);
    
    fs_in_ViewPos = g_view * vec4(vPos.xyz + vs_in_instance.xyz, 1);
    
    
    gl_Position =  g_projection * fs_in_ViewPos;
    
  
    fs_in_worldPos = vs_in_instance.xyz;
    fs_in_depth = gl_Position.z;
    //fs_in_depth = (gl_Position / gl_Position.w).z;
}