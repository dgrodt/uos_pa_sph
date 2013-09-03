#version 150

#include "shader/ShaderGlobals.glslh"

uniform mat4x4 invCamera;

in vec3 vs_in_pos;
in vec3 vs_in_normal;
in vec2 vs_in_tc;
in vec4 vs_in_instance;

out vec2 fs_in_tc;
out vec3 fs_in_normal;
out vec4 gl_Position;
out vec4 fs_in_ViewPos;
out vec3 fs_in_world;

out vec3 reflectedVector, refractedVector;
out float refFactor;
void main()
{

	
    fs_in_normal = vs_in_normal;
    fs_in_tc = vs_in_tc;
    fs_in_world = vs_in_pos;
    
    fs_in_ViewPos = g_view * vec4(vs_in_pos, 1);
    gl_Position =  g_projection * fs_in_ViewPos;

	float freshnelBias;
	float freshnelScale;
	float freshnelPower;
	
	float etaRatio = 0.5f;
	
	vec3 I = normalize(vs_in_pos.xyz - g_eye.xyz);
	vec3 N = normalize(vs_in_normal);
	
	reflectedVector = reflect(I,N);
	refractedVector = refract(I,N,etaRatio);
	float reflectionCoeff = max(0, min(1, 1.5f +  dot(I,-N)));
	refractedVector *= reflectionCoeff;
	refractedVector= vec3(4);

	
}