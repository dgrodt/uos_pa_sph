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
    
    //rotate to normal
    /*vec4 currNorm = g_eye - vec4(vs_in_pos, 0);
    
    vec4 a = vec4(vs_in_normal_new.x, vs_in_normal_new.y, 0, 0);
    vec4 b = vec4(currNorm.x, currNorm.y, 0, 0);
    float cosa_z = dot(a, b)/(length(a)*length(b));
    float sina_z = length(cross(a.xyz, b.xyz))/(length(a)*length(b));
    mat4 zRot = mat4(	cosa_z, sina_z,	0, 		0, 
    					-sina_z,cosa_z,	0, 		0, 
    					0, 		0, 		1, 		0, 
    					0, 		0, 		0, 		1);
    a = vec4(0, vs_in_normal_new.y, vs_in_normal_new.z, 0);
    b = vec4(0, currNorm.y, currNorm.z, 0);
    float cosa_x = dot(a, b)/(length(a)*length(b));
    float sina_x = length(cross(a.xyz, b.xyz))/(length(a)*length(b));
    mat4 xRot = mat4(	1,		0,		0,		0,
    					0,		cosa_x,	sina_x,	0,
    					0,		-sina_x,cosa_x,	0,
    					0,		0,		0,		1);
    a = vec4(0, vs_in_normal_new.y, vs_in_normal_new.z, 0);
    b = vec4(0, currNorm.y, currNorm.z, 0);
    float cosa_y = dot(a, b)/(length(a)*length(b));
    float sina_y = length(cross(a.xyz, b.xyz))/(length(a)*length(b));
    mat4 yRot = mat4(	cosa_y,	0,		-sina_y,0,
    					0,		1,		0,		0,
    					sina_y,	0,		cosa_y,	0,
    					0,		0,		0,		1);
    */
    vec4 vPos = invCamera * vec4(vs_in_pos, 0); //zRot * xRot * yRot * 
    
    fs_in_ViewPos = g_view * vec4(vPos.xyz + vs_in_instance.xyz, 1);
    
    
    gl_Position =  g_projection * fs_in_ViewPos;
    
  
    fs_in_worldPos = vs_in_instance.xyz;
    fs_in_depth = gl_Position.z;
    //fs_in_depth = (gl_Position / gl_Position.w).z;
}